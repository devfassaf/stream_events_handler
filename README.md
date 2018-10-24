# stream_events_handler
The application gets as argument a full path to bigpanda_events_generator that spits out an infinite stream of lines of event data encoded in JSON

the app consumes the data as stream processing service and count of words encountered in the data , event_type fields of the events.

exposes an HTTP API for getting these counters of event_type and data from the events.


API:
http://127.0.0.1:8080/data  - will return an array with all the received data (distinct).

http://127.0.0.1:8080/event_type  - will return an array with all the received event_type (distinct).

http://127.0.0.1:8080/event_type/:type  - will return the number of events by event type.

http://127.0.0.1:8080/data/:data  - will return the number of words encountered in the data field of the events.


Assaf Faybish

linkedin: https://www.linkedin.com/in/assaffaybish/

deffassaf@gmail.com

fassaf.f@gmail.com
