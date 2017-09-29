# Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
from subprocess import call
import subprocess
import time
import os

dir_path = os.path.dirname(os.path.realpath(__file__))


# run node server

# command to run npm
node_cmdline = "npm start"
process = subprocess.Popen("start cmd /K " + node_cmdline, cwd=dir_path, shell=True)
process.wait()
time.sleep(6)


# run java server
server_dir = os.path.join(dir_path, "nazgul-server")

# command to run java server
cmdline = "java -jar " \
          "-DadditionalApps=true " \
          "-DcomplexApp=true " \
          "-DdemoApp=true " \
          "-DallowUnauthenticatedAccess=true " \
          "single-jar-demo.jar"

rc = call("start cmd /K " + cmdline, cwd=server_dir, shell=True)
