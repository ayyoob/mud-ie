import os
import asyncio
import signal
import time
import sys
import ast
from nats.aio.client import Client as NATS
from ryu.exception import RyuException
from ryu.ofproto import ofproto_v1_0
from ryu.ofproto import ofproto_v1_2
from ryu.ofproto import ofproto_v1_3
from ryu.ofproto import ofproto_v1_4
from ryu.ofproto import ofproto_v1_5
from ryu.lib import ofctl_v1_0
from ryu.lib import ofctl_v1_2
from ryu.lib import ofctl_v1_3
from ryu.lib import ofctl_v1_4
from ryu.lib import ofctl_v1_5

supported_ofctl = {
    ofproto_v1_0.OFP_VERSION: ofctl_v1_0,
    ofproto_v1_2.OFP_VERSION: ofctl_v1_2,
    ofproto_v1_3.OFP_VERSION: ofctl_v1_3,
    ofproto_v1_4.OFP_VERSION: ofctl_v1_4,
    ofproto_v1_5.OFP_VERSION: ofctl_v1_5,
}

class NatsClient:
  def __init__(self, logger, dpid_set = None):
    self.url = os.getenv('NATS_URL')
    if dpid_set is not None:
      self.dpset = dpid_set;
    self.logger = logger

  def connect_and_subscribe(self, loop):
    if not self.url:
      self.logger.error('Not connecting to any NATS server, host is None.')
      return

    servers = [self.url]
    options = {
      "io_loop": loop,
      "servers": servers,
    }
    is_connected = False
    while(not is_connected):
      try:
        client = NATS()
        yield from client.connect(**options)
        is_connected = True
      except Exception as e:
        self.logger.error("failed to connect..retrying " + self.url)
        time.sleep(1)
        pass

    def signal_handler():
      if client.is_closed:
        return
      self.logger.info("Disconnecting...")
      loop.create_task(client.close())

    for sig in ('SIGINT', 'SIGTERM'):
      loop.add_signal_handler(getattr(signal, sig), signal_handler)

    @asyncio.coroutine
    def subscribe_handler(msg):
      subject = msg.subject
      reply = msg.reply
      data = msg.data.decode()
      self.logger.debug("Received a message on '{subject} {reply}': {data}".format(
        subject=subject, reply=reply, data=data))
      if data == "reload":
        yield from client.close()
        # loop.create_task(client.close())
        self.logger.info("faucet reload configuration")
        os.kill(os.getpid(), signal.SIGHUP)
      else:
        try:
          body = ast.literal_eval(data)
          dpid = body.get('dpid', None)
          if not dpid:
            self.logger.error("dpid is not found in " + body)
            return
          dp = self.dpset.get(dpid)
          if dp is None:
            self.logger.error('No such Datapath: %s', dpid)
            return
           # Get lib/ofctl_* module
          try:
              ofctl = supported_ofctl.get(dp.ofproto.OFP_VERSION)
              ofctl.mod_flow_entry(dp, body, dp.ofproto.OFPFC_ADD)
          except KeyError:
              self.logger.error('Unsupported OF version: version=%s', dp.ofproto.OFP_VERSION)
        except Exception as e:
          self.logger.error(e)

    self.logger.info("subscribing to faucet.msg topic");
    yield from client.subscribe("faucet.msg", cb=subscribe_handler)

  def subscribe(self):
    asyncio.set_event_loop(asyncio.new_event_loop())
    loop = asyncio.get_event_loop()
    loop.run_until_complete(self.connect_and_subscribe(loop))
    try:
      loop.run_forever()
    finally:
      loop.close()

  def publish_msg(self, subject, msg, loop):
    client = NATS()
    servers = [self.url]
    options = {
      "io_loop": loop,
      "servers": servers,
    }
    try:
      yield from client.connect(**options)
      yield from client.publish(subject, msg)
      yield from client.flush()
      yield from client.close()
    except Exception as e:
      self.logger.error(e)
      pass

  def publish(self, subject, msg):
    asyncio.set_event_loop(asyncio.new_event_loop())
    loop = asyncio.get_event_loop()
    try:
      loop.run_until_complete(self.publish_msg(subject, msg, loop))
    finally:
      loop.close()
