import os
import asyncio
import signal
import time
import sys
import ast
import functools
from nats.aio.client import Client as NATS

class NatsClient:
  def __init__(self):
    self.url = os.getenv('NATS_URL')
    if dpid_set is not None:
      self.dpset = dpid_set;

  def connect_and_subscribe(self, loop):
    if not self.url:
      print('Not connecting to any NATS server, host is None.')
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
        print("failed to connect..retrying " + self.url)
        time.sleep(1)
        pass

    def signal_handler(signal):
      if client.is_closed:
        return
      print("Disconnecting...")
      loop.create_task(client.close())

    for sig in ('SIGINT', 'SIGTERM'):
      loop.add_signal_handler(getattr(signal, sig), signal_handler)

    @asyncio.coroutine
    def subscribe_handler(msg):
      subject = msg.subject
      reply = msg.reply
      data = msg.data.decode()
      print("Received a message on '{subject} {reply}': {data}".format(
        subject=subject, reply=reply, data=data))

    print("subscribing to faucet.msg topic");
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
      print(e)
      pass

  def publish(self, subject, msg):
    asyncio.set_event_loop(asyncio.new_event_loop())
    loop = asyncio.get_event_loop()
    try:
      loop.run_until_complete(self.publish_msg(subject, msg, loop))
    finally:
      loop.close()
