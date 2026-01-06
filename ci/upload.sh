#!/bin/bash
OSSUTIL="./build/ossutil"
UPLOAD_DIR_PATH="./build/bin/"

# try upload file
{
  # config ossutil (endpoint, access_key_id, access_key)
  ${OSSUTIL} config -e $2 -i $3 -k $4

  # upload file
  for file in ./build/bin/*
  do
    file_name=`echo ${file:${#UPLOAD_DIR_PATH}}`
    upload_oss_uripath="$5/backend/java/dentalday/dentalday-admin-api/"

    echo "[INFO]: Upload ${file_name} file......"
    ${OSSUTIL} cp ./build/bin/${file_name} ${upload_oss_uripath} -f
  done
} || {
    echo "[ERROR]: Upload file failed."
    exit 1
}

echo "[INFO]: Upload file successful."
