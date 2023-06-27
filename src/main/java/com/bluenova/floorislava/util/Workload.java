package com.bluenova.floorislava.util;

import com.sk89q.worldedit.WorldEditException;

public interface Workload {

    void compute() throws WorldEditException;

}