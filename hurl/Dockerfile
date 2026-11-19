FROM ghcr.io/orange-opensource/hurl:6.1.1
RUN apk add --no-cache bash postgresql-client
WORKDIR /tests
COPY run.sh /tests/run.sh
COPY tests /tests/tests
COPY sql /tests/sql
ENTRYPOINT ["/bin/bash", "/tests/run.sh"]
CMD ["all"]
