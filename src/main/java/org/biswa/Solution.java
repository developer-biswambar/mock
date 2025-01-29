package org.biswa;

import java.util.HashMap;
import java.util.Map;

class Solution {

    private Map<Integer,String> cache = new HashMap<>();
    public String countAndSay(int n) {
        return helper(n);

    }
    private String helper (int n ){
        if (cache.containsKey(n))
            return cache.get(n);
        if (n==0) return "";

        else if (n==1) return "1";

        String prev = helper(n-1);

        StringBuilder sb = new StringBuilder();
        int i=0;

        while (i<prev.length()){

            int j=i+1;

            while (j<prev.length()&& prev.charAt(j)==prev.charAt(i)){
                j++;
            }
            sb.append(j-i);
            sb.append(prev.charAt(i));
            i=j;

        }
        cache.put(n, sb.toString());
        return sb.toString();

    }
}