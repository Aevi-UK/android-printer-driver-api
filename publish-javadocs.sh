#!/bin/bash
set -e

LOCAL_CLONE_DIR="temp/gh-pages"
JAVA_DOC_LOCATION="printer-driver-api/build/docs/javadoc/"
GIT_USER_NAME="CircleCI"
GIT_USER_EMAIL="brett@annalytics.co.uk"

function exit_with_error {
    echo "ERROR: $2"
    exit $1
}

if [ ! -d $JAVA_DOC_LOCATION ]; then
    exit_with_error 1 "Javadocs have not been built!"
fi

echo "Checking for on master branch"
#if [ ${CIRCLE_BRANCH=local} != "master" ]; then
#    exit_with_error 2 "Not on master branch"
#fi

GITHUB_CLONE_URL=`git remote get-url origin`


rm -f -r $LOCAL_CLONE_DIR

echo "Getting gh-pages branch from GitHub: $LOCAL_CLONE_DIR"
git clone $GITHUB_CLONE_URL $LOCAL_CLONE_DIR
if [ ! -d "$LOCAL_CLONE_DIR" ]; then
    exit_with_error 2 "Failed to get gh-pages branch from GitHub!"
fi

(cd $LOCAL_CLONE_DIR
    git init
    git config user.name $GIT_USER_NAME
    git config user.email $GIT_USER_EMAIL

    git fetch origin
    git checkout gh-pages

    echo "delete all old documentation pages and replace with new javadoc"    
    rm -f -r javadoc/*
    cp  -r "../../$JAVA_DOC_LOCATION/" javadoc/

    echo "Commit and push javadoc to gh-pages"
    git add *

    git commit -a -m "javadoc automatically updated"
    git  push
)


echo "Finished - javadoc docs automatically updated"
