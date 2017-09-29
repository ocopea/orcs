# Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import os
from os import listdir
from os.path import isfile, join
import shutil
import settings
import zipFiles

# configurations from settings.py
SOURCE_DIRNAME = settings.SOURCE_DIRNAME
DESTINATION_DIRNAME = settings.DESTINATION_DIRNAME


# Get all files from source folder (one level deep)
def get_files_from_dir(src_dirname):
    files = []
    for f in listdir(src_dirname):
        if isfile(join(src_dirname, f)):
            files.append(join(src_dirname, f))
        else:
            for thisFile in listdir(join(src_dirname, f)):
                files.append(join(join(src_dirname, f), thisFile))

    return files


# Replace files in destination folder
def replace_files(destination_dirname, source_dirname, files_to_copy):
    # Erase all files in destination folder
    if not is_folder_empty(destination_dirname):
        erase_files_from_folder(destination_dirname)

    for f in files_to_copy:
        # Check if file parent name is equal to settings src name
        # if not create new folder in destination
        parent_dir_abs_path = os.path.abspath(os.path.join(f, os.pardir))
        parent = get_parent_folder_name(parent_dir_abs_path)
        src_dirname = get_parent_folder_name(settings.SOURCE_DIRNAME)

        if parent == src_dirname:

            # file is located in root library
            # copy to destination
            shutil.copy(f, destination_dirname)
        else:
            # file is located in sub folder
            # create folder
            if not os.path.exists(join(destination_dirname, parent)):
                os.mkdir(join(destination_dirname, parent))
                # copy the file to destination sub folder
                for fl in os.listdir(parent_dir_abs_path):
                    src_root_folder = join(fl, source_dirname)
                    src_sub_folder_file = join(join(src_root_folder, parent), fl)
                    dst = join(destination_dirname, parent)

                    shutil.copy(src_sub_folder_file, dst)


# Get parent folder name (by last index of /)
def get_parent_folder_name(path):
    return path[path.rfind('\\')+1::]


# Check if folder is empty
def is_folder_empty(path):
    if os.listdir(path) == "":
        return True
    else:
        return False


# Erase all files in a folder
def erase_files_from_folder(path):
    for f in os.listdir(path):
        if isfile(join(path, f)):
            os.remove(join(path, f))
        else:
            shutil.rmtree(join(path, f))


# Handle files compilation
# copy files and sub directories from settings.SOURCE_DIRNAME to settings.ZIP_FILE_NAME
all_files = get_files_from_dir(SOURCE_DIRNAME)
replace_files(DESTINATION_DIRNAME, SOURCE_DIRNAME, all_files)

# Handle file compression
# compress folder settings.FOLDER_TO_ZIP and output zip file to folder settings.ZIP_FILE_DESTINATION
zipFiles.compress_to_zip(settings.FOLDER_TO_ZIP, settings.ZIP_FILE_DESTINATION, settings.ZIP_FILE_NAME)
