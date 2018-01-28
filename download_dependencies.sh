#!/bin/bash

CURRENT_DIR=$(pwd)
echo "Current dir: $CURRENT_DIR"

PLUGINS_DIR="./lib/plugins/"
echo "Changing to plugins directory: $PLUGINS_DIR"
cd $PLUGINS_DIR

# Downloads a zipped dependency and unpacks its jar contents in the current directory
function download_and_unpack {
    if [ ! -f $1 ]; then
        echo "$1 not found. Trying to fetch it"
        echo "Getting plugin dependency: $2"
        wget -O dependency.zip $2
        echo "unpacking dependency..."
        unzip dependency.zip

        if [ -z "$3" ]; then
            echo "regular lib"
        else
            echo "moving special library"
            mv $3 $1
        fi


        echo "Removing old stuff:"
        rm -r lib
        echo "Cleaning up zip file."
        rm dependency.zip
    else
        echo "Dependency $1 found. Skipping."
    fi
}

function download {
    if [ ! -f $1 ]; then
        echo "Getting plugin dependency: $1"
        wget -O $1 $2
    else
        echo "Dependency $1 found. Skipping."
    fi
}

function get_lpsolve_deps {
    wget https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/LpSolve/LpSolve_5.5.0.15_lin64.zip
    wget https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/LpSolve/LpSolve_5.5.0.15_mac.zip
    wget https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/LpSolve/LpSolve_5.5.0.15_win64.zip
    unzip LpSolve_5.5.0.15_lin64.zip
    unzip LpSolve_5.5.0.15_mac.zip
    unzip LpSolve_5.5.0.15_win64.zip

}
download_and_unpack AcceptingPetriNet.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/AcceptingPetriNet/AcceptingPetriNet-6.7.185-all.zip

download_and_unpack AntiAlignments.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/AntiAlignments/AntiAlignments-6.7.58-all.zip

download_and_unpack ProjectedRecallAndPrecision.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/ProjectedRecallAndPrecision/ProjectedRecallAndPrecision-6.8.148-all.zip

download_and_unpack StochasticPetriNets.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/StochasticPetriNets/StochasticPetriNets-6.8.153-all.zip

download_and_unpack ProcessTree.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/ProcessTree/ProcessTree-6.7.91-all.zip

download_and_unpack EvolutionaryTreeMiner.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/EvolutionaryTreeMiner/EvolutionaryTreeMiner-6.7.171-all.zip

download_and_unpack InductiveMiner.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/InductiveMiner/InductiveMiner-6.8.411-all.zip

download_and_unpack PTConversions.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/PTConversions/PTConversions-6.8.32-all.zip

download_and_unpack PNetReplayer.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/PNetReplayer/PNetReplayer-6.8.169-all.zip

download_and_unpack PetriNets.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/PetriNets/PetriNets-6.7.136-all.zip

download_and_unpack Properties.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/Properties/Properties-6.7.101-all.zip

download_and_unpack Widgets.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/Widgets/Widgets-6.7.230-all.zip

download_and_unpack EfficientStorage.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/EfficientStorage/EfficientStorage-6.8.122-all.zip

get_lpsolve_deps

# ProM - core:
download_and_unpack XESStandard.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/XESStandard/XESStandard-6.8.109-all.zip
download_and_unpack ProM-Plugins.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/ProM-Plugins/ProM-Plugins-6.7.45-all.zip
download_and_unpack ProM-Models.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/ProM-Models/ProM-Models-6.7.20-all.zip
download_and_unpack ProM-Contexts.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/ProM-Contexts/ProM-Contexts-6.8.34-all.zip
download_and_unpack ProM-Framework.jar https://svn.win.tue.nl/trac/prom/export/37720/Releases/Packages/ProM-Framework/ProM-Framework-6.7.52-all.zip

download Cobefra.jar http://processmining.be/cobefra/downloads/cobefra-20171107.jar

download OpenXES.jar https://svn.win.tue.nl/trac/prom/export/latest/Releases/OpenXES/OpenXES-20171120.jar

echo "Going back to the current dir: $CURRENT_DIR"
cd $CURRENT_DIR

