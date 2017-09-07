#!/usr/bin/env bash
if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  echo -e "Starting to update gh-pages\n"

  cp -R build/reports/specs $HOME/specs

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "Travis"
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/Adven27/Exam.git  gh-pages > /dev/null

  cd gh-pages
  git rm -f .
  git commit -m "Travis build $TRAVIS_BUILD_NUMBER remove old files"

  cp -Rf $HOME/specs/* .

  git add -f .
  git commit -m "Travis build $TRAVIS_BUILD_NUMBER pushed to gh-pages"
  git push -fq origin gh-pages > /dev/null

  echo -e "Done magic with specs\n"
fi