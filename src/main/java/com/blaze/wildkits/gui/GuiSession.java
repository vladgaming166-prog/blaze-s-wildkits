package com.blaze.wildkits.gui;

import java.util.HashMap;
import java.util.Map;

public final class GuiSession {

    private GuiType type = GuiType.MAIN;
    private int page;
    private String category;
    private String search;
    private String previewKit;
    private final Map<Integer, String> slotActions = new HashMap<>();

    public GuiType getType() { return type; }
    public void setType(GuiType type) { this.type = type; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = Math.max(0, page); }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }
    public String getPreviewKit() { return previewKit; }
    public void setPreviewKit(String previewKit) { this.previewKit = previewKit; }

    public void clearActions() { slotActions.clear(); }
    public void setAction(int slot, String action) { slotActions.put(slot, action); }
    public String getAction(int slot) { return slotActions.get(slot); }
}
