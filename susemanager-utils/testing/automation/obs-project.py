#!/usr/bin/python3
import argparse
import os
import sys
import urllib.request
import xml.etree.ElementTree as ET
import datetime
import configparser

parser = argparse.ArgumentParser(description="This utility helps you manage an obs project for a Pull Request")
parser.add_argument('--api', help="Build Service API, defaults to https://api.opensuse.org", default="https://api.opensuse.org")
parser.add_argument('--project', help="Project from which to \"branch\" from, defaults to systemsmanagement:Uyuni:Master", default="systemsmanagement:Uyuni:Master")
parser.add_argument('--prproject', help="Project to branch to \"branch\", defaults to systemsmanagement:Uyuni:Master:PR", default="systemsmanagement:Uyuni:Master:PR")
parser.add_argument('pullnumber', help="Pull Request number, for example 1")
parser.add_argument('--configfile', help="Config file where username and password are store, by default ~/.oscrc", default="~/.oscrc")
parser.add_argument('--setmaintainer', help="Set maintainer", default="")


args = parser.parse_args()
api = args.api
project = args.project
pr_project = args.prproject
pull_number = args.pullnumber
maintainer=args.setmaintainer
pr_project = pr_project + ":" + pull_number
config_file = args.configfile

if (not os.path.exists(config_file)):
    print("ERROR: config file {} not found".format(config_file))
    sys.exit(-1)

try:
    config.read(config_file)
except IOError as e:
    print("ERROR: Can't read config file ".format(e))
    sys.exit(-1)

auth_user = config[api]["user"]
auth_passwd = config[api]["pass"]

if (auth_user == "" or auth_passwd == ""):
    print("ERROR: could not find user or password in config file")
    sys.exit(-1)

print("DEBUG: getting api version for debugging purposes")
req = urllib.request.Request("{}/about".format(api))
with urllib.request.urlopen(req) as response:
    data = response.read()
root = ET.fromstring(data)
revision = root.find("revision").text
print("DEBUG: API version: {}".format(revision))

print("DEBUG: getting meta data from {}".format(project))
passman = urllib.request.HTTPPasswordMgrWithDefaultRealm()
url = api + "/source/" + project + "/_meta"
passman.add_password(None, url, auth_user, auth_passwd)
authhandler = urllib.request.HTTPBasicAuthHandler(passman)
opener = urllib.request.build_opener(authhandler)
urllib.request.install_opener(opener)
req = urllib.request.Request(url)
with urllib.request.urlopen(req) as response:
    data = response.read()
root = ET.fromstring(data)
result = root.find("title").text
print("DEBUG: found metadata for project with title {}".format(result))

print("DEBUG: adapting project meta for new project {}".format(pr_project))
root.set("name", pr_project)
new_title = "Build for Pull Request #" + pull_number
print("DEBUG: setting title to {}".format(new_title))
root.find("title").text = new_title 

if (maintainer!=""):
    print("DEBUG: Adding user {} as maintainer".format(auth_user))
    new_person = ET.fromstring("<person userid=\"{}\" role=\"maintainer\"/>".format(auth_user))
    root.append(new_person)

root.find("description").text=str(datetime.datetime.now())

print("DEBUG: adapting list of repositories")
for repo in root.findall("repository"):
    if repo.get("name") == "images":
        print("DEBUG: skipping images repo")
        root.remove(repo)
        continue
    if repo.get("name") == "images_pxe":
        print("DEBUG: skipping images_pxe repo")
        root.remove(repo)
        continue
    for child in repo.findall("path"):
        repo.remove(child)
    for child in repo.findall("releasetarget"):
        repo.remove(child)
    print("DEBUG: Adding setting repository {} to use path {}".format(repo.get("name"), project))
    new_path = ET.fromstring("<path project=\"{}\" repository=\"{}\" />".format(project, repo.get("name")))
    repo.append(new_path)
            

print("DEBUG: creating new project: {}".format(pr_project))
passman = urllib.request.HTTPPasswordMgrWithDefaultRealm()
url = api + "/source/" + pr_project + "/_meta"
passman.add_password(None, url, auth_user, auth_passwd)
authhandler = urllib.request.HTTPBasicAuthHandler(passman)
opener = urllib.request.build_opener(authhandler)
urllib.request.install_opener(opener)
data = ET.tostring(root)
req = urllib.request.Request(url, data = data, method="PUT")
with urllib.request.urlopen(req) as response:
    data = response.read()
root = ET.fromstring(data)
print("DEBUG: result: {}".format(root.get("code")))


