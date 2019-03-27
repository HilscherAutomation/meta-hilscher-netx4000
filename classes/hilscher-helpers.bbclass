
def dir_dep_hash(d, dirList):
    import os
    import hashlib

    hash = ""
    for dir in dirList.split():
        for root, dirs, files in os.walk(dir):
            bb.parse.mark_dependency(d, root)

            for file in files:
                h = hashlib.sha256()
                filename = root+"/"+file
                h.update(filename.encode())
                with open(filename, 'rb') as f:
                    buf = f.read()
                    h.update(buf)
                fileHash = h.hexdigest();
                hash += fileHash

    return hashlib.sha256(hash.encode()).hexdigest()
