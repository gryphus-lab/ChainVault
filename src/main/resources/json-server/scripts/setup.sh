#!/bin/sh
apk add python3 
ln -sf python3 /usr/bin/python
apk add --no-cache py3-pip
apk add --no-cache build-base
apk add --no-cache ttf-dejavu
python3 -m venv .venv && . .venv/bin/activate

pip install --upgrade pip
pip install pipreqs
pipreqs --force /data
pip install -r /data/requirements.txt

echo "Creating source data..."
cd /data || exit 1
python3 create_source_data.py

echo "Starting json-server..."
npm install -g json-server
json-server --watch /data/db.json --static /data/static --port 9091 --host 0.0.0.0