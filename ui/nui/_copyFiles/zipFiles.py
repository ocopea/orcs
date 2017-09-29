# Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import os
import shutil
import zipfile

DIR = os.path.join(os.path.dirname(__file__))


def compress_to_zip(src, dst, file_name):

    zf = zipfile.ZipFile(file_name+".jar", "w", zipfile.ZIP_DEFLATED)
    abs_src = os.path.abspath(src)

    for dirname, subdirs, files in os.walk(src):
        for filename in files:
            absname = os.path.abspath(os.path.join(dirname, filename))
            arcname = absname[len(abs_src) + 1:]

            # print 'zipping %s as %s' % (os.path.join(dirname, filename),
            #                             arcname)+zf.write(dst, arcname)
            zf.write(absname, arcname)

    zf.close()
    copy_zip_file(zf, DIR, dst)


def copy_zip_file(zip_file, src, destination):
    # remove old zip file from destination folder
    for f in os.listdir(destination):
        if f == zip_file.filename:
            os.remove(os.path.join(destination, zip_file.filename))

    # copy zip file from projects base folder
    # to folder defined in settings.ZIP_FILE_DESTINATION
    shutil.copy(os.path.join(src, zip_file.filename), destination)
    # remove zip file from project base folder
    os.remove(os.path.join(src, zip_file.filename))
