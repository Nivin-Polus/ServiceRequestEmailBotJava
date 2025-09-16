package com.servicerequest.emailbot.service.outlook;

import com.servicerequest.emailbot.service.auth.AuthServiceOutlook;
import org.springframework.stereotype.Service;

@Service
public class OutlookAuthService extends AuthServiceOutlook {
    // This class extends the existing AuthServiceOutlook
    // No additional implementation needed - just provides the expected class name
    
    public OutlookAuthService() throws Exception {
        super();
    }
}
