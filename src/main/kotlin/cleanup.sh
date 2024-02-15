#!/bin/bash

# cleans Rust target folders
FOLDERS=$(echo ./*/*/target/)
echo Do you want to delete these folders?
echo $FOLDERS
read
rm -rf $FOLDERS
