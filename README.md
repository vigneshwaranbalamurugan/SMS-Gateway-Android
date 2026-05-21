# SMS Gateway Android
Android app that exposes an HTTP API and sends SMS through the device SIM card.

## Features
- Local HTTP server
- Send SMS via REST API
- Start/Stop server
- Display device IP
- Copy API URL
- Material 3 UI

## API

POST

```http
http://<device-ip>:8080/sendSms
```

Request:

```json
{
  "phone": "9876543210",
  "message": "Hello"
}
```

Response:

```json
{
  "status": "success"
}
```