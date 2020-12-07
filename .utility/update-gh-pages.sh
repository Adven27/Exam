#!/usr/bin/env bash
echo -e "Starting to update gh-pages\n"

cp -R example/build/reports/specs $HOME/specs

cd $HOME
git config --global user.email "${GITHUB_ACTOR}@users.noreply.github.com"
git config --global user.name "${GITHUB_ACTOR}"
git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/Adven27/Exam.git  gh-pages > /dev/null

cd gh-pages
git rm -rf .
git commit -m "${GITHUB_ACTOR} build ${GITHUB_RUN_NUMBER} remove old files"

cp -Rf $HOME/specs/* .

git add -f .
git commit -m "${GITHUB_ACTOR} build ${GITHUB_RUN_NUMBER} pushed to gh-pages"
git push -fq origin gh-pages > /dev/null

echo -e "Update finished\n"