VERSION=$1
mvn versions:set -DnewVersion=$VERSION
mvn clean package -DskipTests
cp flohmarkt.iss.template flohmarkt.iss
sed -i -- "s/##VERSION##/$VERSION/g" flohmarkt.iss
wine ~/.wine/drive_c/Program\ Files\ \(x86\)/Inno\ Setup\ 5/ISCC.exe flohmarkt.iss
git commit -a -m "Bump version to $VERSION"
git remote | xargs -L1 git push
gpa remote | xargs -L1 git push --tags
scp Output/flohmarkthelfer-setup.exe flohmarkthelfer:html/download

