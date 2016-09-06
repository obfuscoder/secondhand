VERSION=$1
mvn versions:set -DnewVersion=$VERSION
rm **/*.versionsBackup
mvn clean package -DskipTests
cp flohmarkt.iss flohmarkt_installer.iss
sed -i -- "s/##VERSION##/$VERSION/g" flohmarkt_installer.iss
wine ~/.wine/drive_c/Program\ Files\ \(x86\)/Inno\ Setup\ 5/ISCC.exe flohmarkt_installer.iss
git commit -a -m "Bump version to $VERSION"
git tag $VERSION
git remote | xargs -L1 git push
git remote | xargs -L1 git push --tags
scp Output/flohmarkthelfer-setup.exe flohmarkthelfer:www/shared/public/download

