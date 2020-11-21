module Main exposing(..)

import Browser
import Browser.Navigation as Nav
import Html exposing (..)
import Html.Attributes exposing (..)
import Url
import Url.Parser as UrlParser exposing ((</>))

main : Program () Model Msg
main =
  Browser.application
    { init = init
    , view = view
    , update = update
    , subscriptions = subscriptions
    , onUrlChange = UrlChanged
    , onUrlRequest = LinkClicked
    }

type Route
  = Home
  | Game String

routeParser : UrlParser.Parser (Route -> a) a
routeParser =
  UrlParser.oneOf
    [ UrlParser.map Home       UrlParser.top
    , UrlParser.map Game      (UrlParser.s "game" </> UrlParser.string)
    ]

type alias Model =
  { key : Nav.Key
  , route: Maybe Route
  }

init : () -> Url.Url -> Nav.Key -> ( Model, Cmd Msg )
init _ url key =
  ( { key = key, route = UrlParser.parse routeParser url }, Cmd.none )

type Msg
  = LinkClicked Browser.UrlRequest
  | UrlChanged Url.Url


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
  case msg of
    LinkClicked urlRequest ->
      case urlRequest of
        Browser.Internal url ->
          ( model, Nav.pushUrl model.key (Url.toString url) )

        Browser.External href ->
          ( model, Nav.load href )

    UrlChanged url ->
      ( { model | route = UrlParser.parse routeParser url }
      , Cmd.none
      )


subscriptions : Model -> Sub Msg
subscriptions _ =
  Sub.none


view : Model -> Browser.Document Msg
view model =
  { title = "rock/paper/scissor Elm"
  , body =
      [ 
        text "The current URL is: "
      , b [] [ text (
                  case model.route of
                    Just Home -> "Home"
                    Just (Game id) -> "Game " ++ id
                    Nothing -> "Not Found"
                  )
              ]
      , ul []
          [ viewLink "/"
          , viewLink "/game"
          , viewLink "/game/1234"
          , viewLink "https://google.com"
          ]
      ]
  }

viewLink : String -> Html msg
viewLink path =
  li [] [ a [ href path ] [ text path ] ]