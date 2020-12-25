/*
 * Copyright 2012 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.netflix.eureka;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;

import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to check whether the eureka server is ready to take requests based on
 * its {@link InstanceStatus}.
 * <p>
 * 在服务器不up的时候 过滤掉请求
 */
@Singleton
public class StatusFilter implements Filter {

    private static final int SC_TEMPORARY_REDIRECT = 307;

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     * javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        // 拿到instanceInfo, 这个instanceInfo代表的是Eureka-Server.
        InstanceInfo myInfo = ApplicationInfoManager.getInstance().getInfo();
        InstanceStatus status = myInfo.getStatus();
        // 如果现在不up了, 就设置给http响应307
        if (status != InstanceStatus.UP && response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(SC_TEMPORARY_REDIRECT,
                    "Current node is currently not ready to serve requests -- current status: "
                            + status + " - try another DS node: ");
        }
        // 如果up状态就过滤下一层.
        chain.doFilter(request, response);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig arg0) throws ServletException {
    }

}
