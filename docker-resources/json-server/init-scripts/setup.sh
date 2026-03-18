#!/usr/bin/env sh

set -eu

readonly VENV_PATH="/opt/venv"
readonly DATA_DIR="/data"

main() {
    echo "--- Installing System Dependencies ---"
    apk add --no-cache \
        python3 \
        py3-pip \
        build-base \
        ttf-dejavu
    ln -sf /usr/bin/python3 /usr/bin/python

    echo "--- Setting up Python Virtual Environment ---"
    python3 -m venv "$VENV_PATH"
    export PATH="$VENV_PATH/bin:$PATH"

    pip install --no-cache-dir --upgrade pip pipreqs

    echo "--- Handling Python Dependencies ---"
    if [ -d "$DATA_DIR" ]; then
        pipreqs --force "$DATA_DIR"
        pip install --no-cache-dir -r "$DATA_DIR/requirements.txt"

        echo "--- Generating Source Data ---"
        (
            cd "$DATA_DIR"
            python3 create_source_data.py --scale 0.25
        )
    else
        echo "Error: $DATA_DIR not found" >&2
        exit 1
    fi

    echo "--- Starting Services ---"
    npm install -g json-server

    exec json-server --watch "$DATA_DIR/db.json" \
         --static "$DATA_DIR/static" \
         --port 9091 \
         --host 0.0.0.0
}

main "$@"
