package com.reedelk.ftp.component;

import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(service = FTPFileFilter.class, scope = ServiceScope.PROTOTYPE)
public class FTPFileFilter implements Implementor {

    @Property("File Name Filter")
    private String fileName;

    @Property("Date Filter")
    private String date;

    @Property("User Filter")
    private String user;


}
