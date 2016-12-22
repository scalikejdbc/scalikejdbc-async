#!/bin/sh

sbt clean "project core" "+ publishSigned" "project playPlugin" "+ publishSigned" #sonatypeRelease

