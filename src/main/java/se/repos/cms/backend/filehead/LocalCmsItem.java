package se.repos.cms.backend.filehead;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import se.simonsoft.cms.item.Checksum;
import se.simonsoft.cms.item.CmsItem;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemKind;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.impl.ChecksumBase;
import se.simonsoft.cms.item.properties.CmsItemProperties;

public class LocalCmsItem implements CmsItem {
    private File file;

    @Override
    public CmsItemId getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RepoRevision getRevisionChanged() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRevisionChangedAuthor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CmsItemKind getKind() {
        if(this.file.isDirectory()) {
            return CmsItemKind.Folder;
        }
        return CmsItemKind.File;
    }

    @Override
    public String getStatus() {
        // Never set on these items.
        return null;
    }

    @Override
    public Checksum getChecksum() {
        final String md5 = this.calculateFileMD5();
        return new ChecksumBase() {
            @Override
            public boolean has(Algorithm a) {
                return a == Algorithm.MD5;
            }

            @Override
            public String getHex(Algorithm a) {
                if (a == Algorithm.MD5) {
                    return md5;
                }
                throw new UnsupportedOperationException();
            }
        };
    }

    private String calculateFileMD5() {
        try {
            return DigestUtils.md5Hex(new FileInputStream(this.file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getCause());
        } catch (IOException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public CmsItemProperties getProperties() {
        // This class has no properties.
        return new CmsItemProperties() {
            @Override
            public String getString(String key) {
                return null;
            }

            @Override
            public List<String> getList(String key) throws ClassCastException {
                return null;
            }

            @Override
            public Set<String> getKeySet() {
                return new HashSet<String>();
            }

            @Override
            public boolean containsProperty(String key) {
                return false;
            }
        };
    }

    @Override
    public long getFilesize() {
        return this.file.getTotalSpace();
    }

    @Override
    public void getContents(OutputStream receiver) throws UnsupportedOperationException {
        try {
            FileInputStream fis = new FileInputStream(this.file);
            IOUtils.copy(fis, receiver);
            IOUtils.closeQuietly(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getCause());
        } catch (IOException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
