set version=$1
mvn versions:set -DnewVersion=$version
mvn clean package -DskipTests
cp flohmarkt.iss.template flohmarkt.iss
sed -i -- "s/##VERSION##/$version/g" flohmarkt.iss
wine ~/.wine/drive_c/Program\ Files\ \(x86\)/Inno\ Setup\ 5/ISCC.exe flohmarkt.iss
gcam "Bump version to $version"
gpa
gpa --tags
scp Output/flohmarkthelfer-setup.exe flohmarkthelfer:html/download

