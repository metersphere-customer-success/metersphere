package io.metersphere.db.entity;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.metersphere.commons.utils.JSON;

@TableName("covs_app")
public class CovsApp {
    public static final int STATUS_OK = 0;
    public static final int STATUS_TRASH = -1;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    @TableField("biz_id")
    private Integer bizId;

    private String token;
    private String src;

    @TableField("git_url")
    private String gitUrl;

    @TableField("git_token")
    private String gitToken;

    @TableField("ver_init_branch")
    private String verInitBranch;

    @TableField("create_time")
    private Timestamp createTime;

    @TableField("update_time")
    private Timestamp updateTime;

    public void init(String name, int bizId, String url, String src, String token) {
        this.name = name;
        this.bizId = bizId;
        this.gitUrl = url;
        this.src = src;
        this.token = token;
        this.verInitBranch = "master";
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBizId() {
        return this.bizId;
    }

    public void setBizId(Integer bizId) {
        this.bizId = bizId;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSrc() {
        return this.src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getGitUrl() {
        return this.gitUrl;
    }

    public void setGitUrl(String url) {
        this.gitUrl = url;
    }

    public String getGitToken() {
        return this.gitToken;
    }

    public void setGitToken(String token) {
        this.gitToken = token;
    }

    public String getVerInitBranch() {
        return this.verInitBranch;
    }

    public void setVerInitBranch(String branch) {
        this.verInitBranch = branch;
    }

    // public JsonObject toJson() {
    // // JSONObject json = new JSONObject();
    // // json.sput("id", this.id);
    // // json.sput("name", this.name);
    // // json.sput("biz_id", this.bizId);
    // // json.sput("token", this.token);
    // // json.sput("src", this.src);
    // // json.sput("git_url", this.gitUrl);
    // // json.sput("git_token", this.gitToken);
    // // json.sput("ver_init_branch", this.verInitBranch);
    // // json.sput("create_time", this.createTime);
    // // json.sput("update_time", this.updateTime);
    // return null;
    // }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.id);
        map.put("name", this.name);
        map.put("biz_id", this.bizId);
        map.put("token", this.token);
        map.put("src", this.src);
        map.put("git_url", this.gitUrl);
        map.put("git_token", this.gitToken);
        map.put("ver_init_branch", this.verInitBranch);
        map.put("create_time", this.createTime);
        map.put("update_time", this.updateTime);
        return map;
    }

    public String toString() {
        return JSON.toJSONString(this.toMap());
    }
}
