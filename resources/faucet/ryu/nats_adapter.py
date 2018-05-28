import errno
import os
import select
import socket
import sys
from threading import Thread
from ryu.base import app_manager
from ryu.controller import dpset
from ryu.controller import ofp_event
from ryu.controller.handler import CONFIG_DISPATCHER, MAIN_DISPATCHER
from ryu.controller.handler import set_ev_cls
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

    #This should not handle any packet in, if there is any packets are sent then push a rule to forward those packets.
    # @set_ev_cls(ofp_event.EventOFPPacketIn, MAIN_DISPATCHER)
    # def packetin_handler(self, ev):
    #     datapath = ev.msg.datapath
    #     ofproto = datapath.ofproto
    #     parser = datapath.ofproto_parser
    #     match = parser.OFPMatch()
    #     actions = [parser.OFPActionOutput(ofproto.OFPP_NORMAL, 0)]
    #     self.add_flow(datapath, match, actions)


    # def add_flow(self, datapath, match, act,table_id = 0, priority=1, idle_timeout=0,hard_timeout=0):
    #     ofproto = datapath.ofproto
    #     parser = datapath.ofproto_parser
    #     inst = [parser.OFPInstructionActions(ofproto.OFPIT_APPLY_ACTIONS, act)]
    #     mod = parser.OFPFlowMod(datapath=datapath,table_id=table_id,  cookie = 7730494, command = ofproto.OFPFC_ADD, priority=priority,
    #         match=match, instructions=inst, idle_timeout=idle_timeout, hard_timeout=hard_timeout)
    #     datapath.send_msg(mod)
