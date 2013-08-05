#!/bin/sh

sbt clean "project core" publish "project play-plugin" publish

