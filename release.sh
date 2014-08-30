#!/bin/sh

sbt clean "project core" "+ publishSigned" "project play-plugin" "+ publishSigned"

