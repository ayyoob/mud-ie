FROM faucet/faucet-base

ENV PYTHONUNBUFFERED=0

RUN apk add --update \
    python3 \
    python3-dev \
    py3-pip \
    gcc \
    musl-dev \
    && rm -rf /var/cache/apk/*

WORKDIR /src

COPY requirements.txt requirements.txt
RUN pip3 install -r requirements.txt

COPY nats_adapter.py nats_adapter.py
COPY nats_client.py nats_client.py

CMD ["python3", "nats_adapter.py"]