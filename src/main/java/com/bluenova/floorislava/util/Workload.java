package com.bluenova.floorislava.util;

import com.sk89q.worldedit.WorldEditException;

public abstract class Workload {

    protected abstract void compute() throws WorldEditException;
}