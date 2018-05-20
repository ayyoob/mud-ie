FROM faucet/faucet-python3

ENV PYTHONUNBUFFERED=0

RUN apk add --update \
    python3 \
    python3-dev \
    py3-pip \
    gcc \
    musl-dev \
    && rm -rf /var/cache/apk/*

WORKDIR /home/ryu-extension

COPY requirements.txt requirements.txt
RUN pip3 install -r requirements.txt

COPY nats_adapter.py nats_adapter.py
COPY nats_client.py nats_client.py

CMD ["ryu-manager", "/home/ryu-extension/nats_adapter.py"]