#!/bin/bash

mkdir -p debug

dot -Tpng debug/dominator_tree.dot > debug/dominator_tree.png

dot -Tpng debug/cfg_before_ssa.dot > debug/cfg_before_ssa.png
dot -Tpng debug/cfg_after_ssa_generation.dot > debug/cfg_after_ssa_generation.png
