echo -e "Uploading jar to site"
git config --global user.email "jgoett154@gmail.com"
git config --global user.name "legobear154"

git clone -q ${GH_TOKEN}@github.com:legobear154/legobear154.github.io.git
cd legobear154.github.io
mv "../build/libs/MyTown-1.6-beta.jar" "./MyTown-BETA-$TRAVIS_BUILD_NUMBER.jar"
git add --all
git commit -q -m "Build number $TRAVIS_BUILD_NUMBER"
git push -fq origin master

echo -e "Upload compelte"