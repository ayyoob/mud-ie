FROM faucet/faucet

ENV PYTHONUNBUFFERED=0

RUN apk add --update \
    python3 \
    python3-dev \
    py3-pip \
    gcc \
    musl-dev \
    && rm -rf /var/cache/apk/*

WORKDIR /home/faucet

COPY requirements.txt requirements.txt
RUN pip3 install -r requirements.txt

COPY nats_adapter.py nats_adapter.py
COPY nats_client.py nats_client.py

CMD ["faucet", "--ryu-app", "/home/faucet/nats_adapter.py"]