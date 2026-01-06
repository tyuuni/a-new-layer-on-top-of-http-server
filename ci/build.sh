# clean folder
rm -rf build && rm -rf _temp_bin
# build
./gradlew installDist
# compress folder
mv build/install/dentalday-admin-api ./ && rm -rf build
mkdir ./_temp_bin
cd dentalday-admin-api && tar czvf ../_temp_bin/build.tar.gz . >/dev/null 2>&1
cd ..
mkdir -p ./build/bin && cp ./_temp_bin/build.tar.gz ./build/bin
# clean folder
rm -rf _temp_bin dentalday-admin-api
