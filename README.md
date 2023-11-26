# stress-aware-robot

Source code for the "Stress Aware Robot" developed by a group of students at University of Oulu as part of the "Advanced Computing Project II" Course.

## Modules

### Pepper

This is an Android project using the Pepper Qi SDK 7. It implements the main logic of the application and connects to the services provided by DRS.

### API

This module contains a PHP HTTP API and an SQLite file that serves as the database of this application.

### DRS

DRS (Docker Robot Server) from [Crowd-Computing-Oulu/drs](https://github.com/Crowd-Computing-Oulu/drs/). This is a set of dockerised services that provide speech recognition, speech synthesis and a chatbot service as HTTP APIs.

## Services

| Module | Service                  | Purpose                                                                  | Internal Port | External Port | Public Endpoint |
|--------|--------------------------|--------------------------------------------------------------------------|---------------|---------------|-----------------|
| API    | PHP Web UI               | Manage data in SQL database, moderate user input, query usage statistics | 8080          | 80/443        | /gui            |
| API    | PHP Database API         | The robot writes and reads data through this API                         | 8080          | 80/443        | /db             |
| DRS    | Bark                     | Synthesise speech from text (text input, audio output)                   | 9000          | 80/443        | /tts            |
| DRS    | Whisper.CPP              | Recognise speech from audio (audio input, text output)                   | 9001          | 80/443        | /stt            |
| DRS    | Postgres for Whisper.CPP | Utility service for Whisper.CPP                                          | 5432          | 80/443        | none            |
| DRS    | Rasa                     | Chatbot service (text input, text output)                                | 5005          | 80/443        | /chat           |

