#!/bin/bash

dot -Tpng debug/dominator_tree.dot > debug/dominator_tree.png
dot -Tpng debug/cfg.dot > debug/cfg.png
