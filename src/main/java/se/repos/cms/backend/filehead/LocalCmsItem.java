package se.repos.cms.backend.filehead;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import se.simonsoft.cms.item.Checksum;
import se.simonsoft.cms.item.CmsItem;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemKind;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.impl.ChecksumBase;
import se.simonsoft.cms.item.impl.CmsItemIdUrl;
import se.simonsoft.cms.item.properties.CmsItemProperties;

public class LocalCmsItem implements CmsItem {
    private CmsRepository repository;
    private CmsItemPath path;
    
    public LocalCmsItem(CmsRepository repository, CmsItemPath path) {
        this.repository = repository;
        this.path = path;
    }

    private File getItemFile() {
        String filePath = this.repository.getPath() + this.path.getPath();
        return new File(filePath);
    }

    @Override
    public CmsItemId getId() {
        return new CmsItemIdUrl(this.repository, this.path);
    }

    @Override
    public RepoRevision getRevisionChanged() {
        long lastModified = this.getItemFile().lastModified();
        return new LocalRepoRevision(new Date(lastModified));
    }

    @Override
    public String getRevisionChangedAuthor() {
        // TODO How to save the author?
        return null;
    }

    @Override
    public CmsItemKind getKind() {
        if (this.getItemFile().isDirectory()) {
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
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(this.getItemFile());
            return DigestUtils.md5Hex(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getCause());
        } catch (IOException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            IOUtils.closeQuietly(fis);
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
                return null;
            }

            @Override
            public boolean containsProperty(String key) {
                return false;
            }
        };
    }

    @Override
    public long getFilesize() {
        return this.getItemFile().getTotalSpace();
    }

    @Override
    public void getContents(OutputStream receiver) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(this.getItemFile());
            IOUtils.copy(fis, receiver);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getCause());
        } catch (IOException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }
}
