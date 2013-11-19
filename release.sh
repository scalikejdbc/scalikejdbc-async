#!/bin/sh

sbt clean "project core" publish-signed "project play-plugin" publish-signed

