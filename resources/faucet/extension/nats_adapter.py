
import errno
import os
import select
import socket
import sys
from threading import Thread
from ryu.base import app_manager
from nats_client import NatsClient
import time

def get_sys_prefix():
    """This was copied from faucet.valve_util.
    Returns an additional prefix for log and configuration files when used in
    a virtual environment"""

    # Find the appropriate prefix for config and log file default locations
    # in case Faucet is run in a virtual environment. virtualenv marks the
    # original path in sys.real_prefix. If this value exists, and is
    # different from sys.prefix, then we are most likely running in a
    # virtualenv. Also check for Py3.3+ pyvenv.
    sysprefix = ''
    if (getattr(sys, 'real_prefix', sys.prefix) != sys.prefix or
            getattr(sys, 'base_prefix', sys.prefix) != sys.prefix):
        sysprefix = sys.prefix

    return sysprefix

class NatsAdapter(app_manager.RyuApp):
    def __init__(self, *args, **kwargs):
        super(NatsAdapter, self).__init__(*args, **kwargs)
        NatsAdapter.dpset = kwargs['dpset']
        self.event_sock = os.getenv('FAUCET_EVENT_SOCK', '0')
        # setup socket
        self.sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
        thread = Thread(target=self.initialize_faucet_event_listener)
        thread.start()
        subscriber = NatsClient();
        subscriberThread = Thread(target=subscriber.subscribe)
        subscriberThread.start()


    def socket_conn(self):
        """Make connection to sock to receive events"""
        # check if socket events are enabled
        if self.event_sock == '0':
            print('Not connecting to any socket, FA_EVENT_SOCK is none.')
            return False
        if self.event_sock == '1':
            self.event_sock = get_sys_prefix() + '/var/run/faucet/faucet.sock'
        # otherwise it's a path

        #waitng for faucet to initialize
        time.sleep(10)
        # create connection to unix socket
        try:
            self.sock.connect(self.event_sock)
        except socket.error as err:
            print("Failed to connect to the socket because: %s" % err)
            return False
        print("Connected to the socket at %s" % self.event_sock)
        return True


    def initialize_faucet_event_listener(self):
        """Make connections to sock and rabbit and receive messages from sock
        to sent to rabbit
        """
        # ensure connections to the socket and rabbit before getting messages
        if self.socket_conn():
            # get events from socket
            self.sock.setblocking(0)
            recv_data = True
            buffer = b''
            while recv_data:
                if not buffer:
                    read_ready, _, _ = select.select([self.sock], [], [])
                    if self.sock in read_ready:
                        continue_recv = True
                        while continue_recv:
                            try:
                                buffer += self.sock.recv(1024)
                            except socket.error as err:
                                if err.errno != errno.EWOULDBLOCK:
                                    recv_data = False
                                continue_recv = False
                # send events to rabbit
                try:
                    nats_client = NatsClient();
                    buffers = buffer.strip().split(b'\n')
                    for buff in buffers:
                        nats_client.publish("faucet.sock.stream", buff)
                    buffer = b''
                except Exception as err:
                    print("Unable to send event to NATS because: %s" % err)
                sys.stdout.flush()
            self.sock.close()

    def dpid_log(dpid):
        """Log a DP ID as hex/decimal."""
        return 'DPID %u (0x%x)' % (dpid, dpid)
