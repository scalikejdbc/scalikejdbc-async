#!/bin/sh

sbt clean "project core" "+ publishSigned" sonatypeRelease

