#!/bin/bash

mkdir -p debug

dot -Tpng debug/dominator_tree.dot > debug/dominator_tree.png

dot -Tpng debug/cfg_before_ssa.dot > debug/cfg_before_ssa.png
dot -Tpng debug/cfg_post_ssa.dot > debug/cfg_post_ssa.png
dot -Tpng debug/cfg_ssa_before_optimizer.dot > debug/cfg_ssa_before_optimizer.png
