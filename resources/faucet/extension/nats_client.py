import os
import asyncio
import signal
from nats.aio.client import Client as NATS


class NatsClient:
  def __init__(self):
    self.url = os.getenv('NATS_URL')

  def connect_and_subscribe(self, loop):
    if not self.url:
      print('Not connecting to any NATS server, host is None.')
      return

    @asyncio.coroutine
    def subscribe_handler(msg):
      subject = msg.subject
      reply = msg.reply
      data = msg.data.decode()
      print("Received a message on '{subject} {reply}': {data}".format(
        subject=subject, reply=reply, data=data))

    options = {
      "io_loop": loop,
      "servers": [self.url],
    }
    try:
      client = NATS()
      yield from client.connect(options)
    except Exception as e:
      pass


    def signal_handler():
      if client.is_closed:
        return
      print("Disconnecting...")
      loop.create_task(client.close())

    for sig in ('SIGINT', 'SIGTERM'):
      loop.add_signal_handler(getattr(signal, sig), signal_handler)

    yield from client.subscribe("ryu.msg", cb=subscribe_handler)

  def subscribe(self):
    asyncio.set_event_loop(asyncio.new_event_loop())
    loop = asyncio.get_event_loop()
    try:
      loop.run_until_complete(self.connect_and_subscribe(loop))
    finally:
      loop.close()

  def publish_msg(self, subject, msg, loop):
    client = NATS()
    options = {
      "io_loop": loop,
      "servers": [self.url],
    }
    try:
      yield from client.connect(options)
      yield from client.publish(subject, msg.encode())
      yield from client.flush()
      yield from client.close()
    except Exception as e:
      pass

  def publish(self, subject, msg):
    asyncio.set_event_loop(asyncio.new_event_loop())
    loop = asyncio.get_event_loop()
    try:
      loop.run_until_complete(self.publish_msg(subject, msg, loop))
    finally:
      loop.close()

  def _send_flow_msgs(self, dp_id, flow_msgs):
    """Send OpenFlow messages to a connected datapath.

    Args:
        dp_id (int): datapath ID.
        flow_msgs (list): OpenFlow messages to send.
        ryu_dp: Override datapath from DPSet.
    """
    ryu_dp = NatsAdapter.dpset.get(dp_id)
    if not ryu_dp:
      self.logger.error('send_flow_msgs: %s not up', self.dpid_log(dp_id))
      return

    for flow_msg in flow_msgs:
      flow_msg.datapath = ryu_dp
      ryu_dp.send_msg(flow_msg)

