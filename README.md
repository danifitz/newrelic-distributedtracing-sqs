# newrelic-distributedtracing-sqs

Simple code snippets for two Java apps `service.a` and `service.b` which send and receive
messages using AWS SQS and pass a Distributed Tracing payload along with the message
to enable New Relic to assemble a trace across two distributed services.
