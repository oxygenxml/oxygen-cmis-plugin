git config user.name "Alex Jitianu";
git config user.email "alex_jitianu@oxygenxml.com";
git fetch;
git checkout master;
git reset;
cp -rf target/addon/* build;
git add build;
git commit -m "New release - ${TRAVIS_TAG}";
git push origin HEAD:master; 
