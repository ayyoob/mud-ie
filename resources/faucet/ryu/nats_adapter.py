
import errno
import os
import select
import socket
import sys
from threading import Thread
from ryu.base import app_manager
from ryu.controller import dpset
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
    _CONTEXTS = {
        'dpset': dpset.DPSet
    }

    def __init__(self, *args, **kwargs):
        super(NatsAdapter, self).__init__(*args, **kwargs)
        dpset = kwargs['dpset']
        subscriber = NatsClient(self.logger,dpset);
        subscriberThread = Thread(target=subscriber.subscribe)
        subscriberThread.start()