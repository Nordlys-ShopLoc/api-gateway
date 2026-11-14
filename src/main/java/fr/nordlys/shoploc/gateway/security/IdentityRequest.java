package fr.nordlys.shoploc.gateway.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class IdentityRequest extends HttpServletRequestWrapper {

    private static final String USER_ID = "X-User-Id";
    private static final String USER_ROLE = "X-User-Role";

    private final String userId;
    private final String role;

    public IdentityRequest(HttpServletRequest request, String userId, String role) {
        super(request);
        this.userId = userId;
        this.role = role;
    }

    @Override
    public String getHeader(String name) {
        if (USER_ID.equalsIgnoreCase(name)) {
            return userId;
        }
        if (USER_ROLE.equalsIgnoreCase(name)) {
            return role;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (isIdentityHeader(name)) {
            String value = getHeader(name);
            if (value == null) {
                return Collections.emptyEnumeration();
            }
            return Collections.enumeration(List.of(value));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = new ArrayList<>();
        for (String name : Collections.list(super.getHeaderNames())) {
            if (!isIdentityHeader(name)) {
                names.add(name);
            }
        }
        if (userId != null) {
            names.add(USER_ID);
        }
        if (role != null) {
            names.add(USER_ROLE);
        }
        return Collections.enumeration(names);
    }

    private boolean isIdentityHeader(String name) {
        return USER_ID.equalsIgnoreCase(name) || USER_ROLE.equalsIgnoreCase(name);
    }
}
