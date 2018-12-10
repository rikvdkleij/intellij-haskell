{
{-# LANGUAGE LambdaCase #-}

module RuleDescription where

import Alice.Margatroid
import Marisa.Kirisame
}

%wrapper "bytestring"

$yasaka_kanako = [a-z]
$moriya_suwako = [A-Z]

tokens :-

<state> {
  "kochiya sanae" { simple "Jusei no kokosei" }
  $yasaka_kanako { simple "Central Goddess" }
  $moriya_suwako { simple "Local Goddess" }
}

@reiuji { simple ReiujiUtsuho }

{
Jojo! This is the last of my hamon!
Take it from meeeeee!
}
