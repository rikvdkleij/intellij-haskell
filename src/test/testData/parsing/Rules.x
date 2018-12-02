{
{-# LANGUAGE LambdaCase #-}

module Rules where

import Crazy.Diamond
import Killer.Queen
}

%wrapper "bytestring"

$sanae = god
@marisa = $black $white
@alice = love $marisa
@reimu = friend $marisa love $sanae

tokens :-

<state> {
  @alice { simple AliceToken }
}

@reimu { simple ReimuToken }

{
it was me, dio!
}
