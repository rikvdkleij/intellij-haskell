module OneLineLetIn where

fix f = let x = f x in x
